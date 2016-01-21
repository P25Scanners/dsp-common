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

package org.anhonesteffort.dsp;

public class Oscillator {

  private final ComplexNumber incrementFactor;
  private       ComplexNumber currentAngle;

  public Oscillator(long sampleRate, double frequency, ComplexNumber initialAngle) {
    incrementFactor = new ComplexNumber((float) (2.0d * Math.PI * frequency / (double) sampleRate));
    currentAngle    = initialAngle;
  }

  public Oscillator(long sampleRate, double frequency) {
    this(sampleRate, frequency, new ComplexNumber(1f, 0f));
  }

  public ComplexNumber next() {
    currentAngle = currentAngle.multiply(incrementFactor).normalize();
    return currentAngle.copy();
  }

}

