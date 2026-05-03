package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.api.CampaignRequestDto;
import com.coddicted.buzzma.campaign.entity.Platform;
import com.coddicted.buzzma.campaign.entity.Product;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(source = "productBrandName", target = "name")
  @Mapping(source = "productImageUrl", target = "imageUrl", qualifiedByName = "stringToUrl")
  @Mapping(source = "productUrl", target = "productLink", qualifiedByName = "stringToUrl")
  @Mapping(source = "originalPricePaise", target = "pricePaise")
  @Mapping(source = "platform", target = "platform", qualifiedByName = "stringToPlatform")
  Product toProductEntity(final CampaignRequestDto request);

  @Named("stringToUrl")
  default URL stringToUrl(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return URI.create(value).toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL: " + value, e);
    }
  }

  @Named("stringToPlatform")
  default Platform stringToPlatform(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return Platform.valueOf(value.toUpperCase());
  }
}
