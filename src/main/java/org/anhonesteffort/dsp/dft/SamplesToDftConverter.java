/*
 * Copyright (C) 2015 An Honest Effort LLC, coping.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.anhonesteffort.dsp.dft;

import org.anhonesteffort.dsp.ConcurrentSource;
import org.anhonesteffort.dsp.Sink;
import org.anhonesteffort.dsp.StreamInterruptedException;
import org.anhonesteffort.dsp.sample.Samples;
import org.jtransforms.fft.FloatFFT_1D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.FloatBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SamplesToDftConverter extends ConcurrentSource<DftFrame, Sink<DftFrame>> implements Sink<Samples> {

  private static final Logger log = LoggerFactory.getLogger(SamplesToDftConverter.class);

  private final BlockingQueue<FloatBuffer> consumedSamples;
  private final int                        dftLength;
  private final FloatFFT_1D                fft;

  public SamplesToDftConverter(DftWidth dftWidth, int queueSize) {
    consumedSamples = new LinkedBlockingQueue<>(queueSize);
    dftLength       = dftWidth.getWidth() * 2;
    fft             = new FloatFFT_1D(dftWidth.getWidth());
  }

  @Override
  public void consume(Samples samples) {
    if (!consumedSamples.offer(samples.getSamples())) {
      consumedSamples.clear();
      consumedSamples.add(samples.getSamples());
      log.warn("sample receive queue has overflowed");
    }
  }

  private class DftFrameSlicer implements Supplier<float[]> {
    private float[] queuedSamples      = new float[dftLength];
    private int     queuedSamplesIndex = 0;

    @Override
    public float[] get() {
      try {

        float[] frame      = new float[dftLength];
        int     frameIndex = 0;

        while (frameIndex < dftLength && !Thread.interrupted()) {
          while (frameIndex < dftLength && queuedSamplesIndex < queuedSamples.length) {
            frame[frameIndex++] = queuedSamples[queuedSamplesIndex++];
          }
          if (frameIndex < dftLength) {
            FloatBuffer buffer = consumedSamples.take();

            if (buffer.hasArray()) {
              queuedSamples = buffer.array();
            } else {
              queuedSamples = new float[buffer.remaining()];
              buffer.get(queuedSamples);
            }

            queuedSamplesIndex = 0;
          }
        }

        return frame;

      } catch (InterruptedException e) {
        throw new StreamInterruptedException("interrupted while slicing dft frame", e);
      }
    }
  }

  @Override
  public Void call() throws StreamInterruptedException {
    try {

      Stream.generate(new DftFrameSlicer()).forEach(frame -> {
        fft.complexForward(frame);
        broadcast(new DftFrame(frame));
      });

    } finally {
      consumedSamples.clear();
    }

    return null;
  }

}
