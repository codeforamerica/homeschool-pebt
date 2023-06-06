package org.homeschoolpebt.app.data;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.stereotype.Component;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Data
@Table(name = "transmissions")
@Component
public class Transmission {
  @Id
  @GeneratedValue
  private UUID id;
}
