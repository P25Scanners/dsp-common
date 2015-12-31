package org.anhonesteffort.dsp.filter;

import org.anhonesteffort.dsp.ComplexNumber;

public class NoOpComplexNumberFilter extends ConsistentRateFilter<ComplexNumber> {

  @Override
  protected ComplexNumber getNextOutput(ComplexNumber input) {
    return input;
  }

}
