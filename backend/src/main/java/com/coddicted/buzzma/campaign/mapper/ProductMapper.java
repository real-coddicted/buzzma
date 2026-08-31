package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.shared.enums.Platform;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(source = "productName", target = "name")
  @Mapping(source = "productBrandName", target = "brandName")
  @Mapping(source = "productImageUrl", target = "imageUrls", qualifiedByName = "stringToUrlList")
  @Mapping(source = "productUrl", target = "productLink", qualifiedByName = "stringToUrl")
  @Mapping(source = "originalPricePaise", target = "pricePaise")
  Product toProductEntity(final CampaignRequestDto request);

  @Named("stringToUrlList")
  default List<URL> stringToUrlList(final String value) {
    final URL url = stringToUrl(value);
    return url == null ? List.of() : List.of(url);
  }

  @Named("stringToUrl")
  default URL stringToUrl(final String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return URI.create(value).toURL();
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL: " + value, e);
    }
  }

  @Named("stringToPlatform")
  default Platform stringToPlatform(final String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return Platform.valueOf(value.toUpperCase());
  }
}
