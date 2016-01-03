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

package org.anhonesteffort.dsp.util;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.stream.IntStream;

public class CircularFloatAveragingQueue {

  private final CircularFifoQueue<float[]> queue;
  private int arrayLength = 0;

  public CircularFloatAveragingQueue(int size) {
    queue = new CircularFifoQueue<>(size);
  }

  public boolean isEmpty() {
    return queue.isEmpty();
  }

  public void add(float[] floats) {
    queue.add(floats);
    arrayLength = floats.length;
  }

  public float[] remove() {
    float[] averages = new float[arrayLength];

    IntStream.range(0, arrayLength).forEach(
        arrayIndex -> averages[arrayIndex] = (float) IntStream.range(0, queue.size()).mapToDouble(
            queueIndex -> (double) queue.get(queueIndex)[arrayIndex]
        ).average().getAsDouble()
    );

    queue.remove();
    return averages;
  }

  public void clear() {
    queue.clear();
  }

}
