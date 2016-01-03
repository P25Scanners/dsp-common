package org.anhonesteffort.dsp.sample;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public class TunableSamplesSourceFactory {

  private List<TunableSamplesSource> sources = new LinkedList<>();

  public TunableSamplesSourceFactory() {
    ServiceLoader.load(TunableSamplesSourceProvider.class).forEach(provider -> {
        Optional<TunableSamplesSource> source = provider.get();
        if (source.isPresent())
          sources.add(source.get());
    });
  }

  public List<TunableSamplesSource> get() {
    return sources;
  }

}
