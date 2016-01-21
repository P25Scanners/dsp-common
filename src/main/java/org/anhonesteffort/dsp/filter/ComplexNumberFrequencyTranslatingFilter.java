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
import org.anhonesteffort.dsp.Oscillator;

public class ComplexNumberFrequencyTranslatingFilter extends ConsistentRateFilter<ComplexNumber> {

  private Oscillator oscillator;

  public ComplexNumberFrequencyTranslatingFilter(long sourceRate, double sourceFreq, double channelFreq) {
    oscillator = new Oscillator(sourceRate, sourceFreq - channelFreq);
  }

  @Override
  protected ComplexNumber getNextOutput(ComplexNumber input) {
    return oscillator.next().multiply(input);
  }

}
