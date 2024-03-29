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

package org.anhonesteffort.dsp.filter.rate;

import org.anhonesteffort.dsp.ComplexNumber;
import org.anhonesteffort.dsp.Sink;

public class ComplexNumberResamplingFilter extends RateChangeFilter<ComplexNumber> {

  private final RateChangeFilter<ComplexNumber> interpolator;
  private final RateChangeFilter<ComplexNumber> decimator;

  public ComplexNumberResamplingFilter(RateChangeFilter<ComplexNumber> interpolator,
                                       RateChangeFilter<ComplexNumber> decimator)
  {
    super(interpolator.getInterpolation(), decimator.getDecimation());

    this.interpolator = interpolator;
    this.decimator    = decimator;

    interpolator.addSink(decimator);
  }

  @Override
  public void consume(ComplexNumber element) {
    interpolator.consume(element);
  }

  @Override
  public void addSink(Sink<ComplexNumber> sink) {
    decimator.addSink(sink);
  }

  @Override
  public void removeSink(Sink<ComplexNumber> sink) {
    decimator.removeSink(sink);
  }

}
