package com.coddicted.buzzma.connection.model;

import com.coddicted.buzzma.connection.entity.Connection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ConnectionView {
  private Connection connection;
  private String fromName;
  private String toName;
}
