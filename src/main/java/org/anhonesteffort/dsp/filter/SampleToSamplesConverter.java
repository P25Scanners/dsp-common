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

package org.anhonesteffort.dsp.filter;

import org.anhonesteffort.dsp.ComplexNumber;
import org.anhonesteffort.dsp.Source;
import org.anhonesteffort.dsp.sample.DynamicSink;
import org.anhonesteffort.dsp.sample.Samples;

import java.nio.FloatBuffer;

public class SampleToSamplesConverter extends Source<Samples, DynamicSink<Samples>> implements DynamicSink<ComplexNumber> {

  @Override
  public void onSourceStateChange(Long sampleRate, Double frequency) {
    sinks.forEach(sink -> sink.onSourceStateChange(sampleRate, frequency));
  }

  @Override
  public void consume(ComplexNumber element) {
    broadcast(new Samples(
        FloatBuffer.wrap(new float[] {element.getInPhase(), element.getQuadrature()})
    ));
  }

}
