package org.homeschoolpebt.app.utils;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SchoolListParser {
  private static final String CSV_PATH = "src/main/resources/school-combobox.csv";
  private static List<Item> items;

  @Data
  @NoArgsConstructor
  public static class Item {
    @CsvBindByName
    public String value;
    @CsvBindByName
    public Long cdsCode;
    @CsvBindByName
    public String displayName;
    @CsvBindByName
    public Boolean isCEP;
  }

  private static void load() {
    if (items != null) {
      return;
    }

    try {
      var items = new CsvToBeanBuilder<Item>(new FileReader(CSV_PATH))
        .withType(Item.class)
        .build()
        .parse();

      SchoolListParser.items = items;
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Item> getItems() {
    load();
    return items;
  }

  public static Set<Long> cdsCodesForCepSchools() {
    return getItems().stream().filter(item -> item.isCEP).map(item -> item.cdsCode).collect(Collectors.toSet());
  }
}
