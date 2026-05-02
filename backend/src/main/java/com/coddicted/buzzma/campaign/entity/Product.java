package com.coddicted.buzzma.campaign.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import jakarta.persistence.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "products")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Product {
  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  UUID id;

  @Column(name = "name", updatable = false, nullable = false)
  String name;

  @Column(name = "image_url", updatable = false, nullable = false)
  URL imageUrl;

  @Column(name = "product_link", updatable = false, nullable = false)
  URL productLink;

  @Column(name = "price_paise", updatable = false, nullable = false)
  BigInteger pricePaise;

  @Enumerated(EnumType.STRING)
  @Column(name = "platform", nullable = false)
  private Platform platform;
}
