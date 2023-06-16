package org.homeschoolpebt.app.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public class SchoolListUtilities {
  @Data
  @AllArgsConstructor
  static class ComboboxItem {
    public String displayName;
    public String label;
    public String value;
  }

  public static List<ComboboxItem> schoolsForCombobox() {
    return SchoolListParser
      .getItems()
      .stream()
      .map(school -> new ComboboxItem(school.getDisplayName(), school.getDisplayName(), school.getValue()))
      .toList();
  }

  public static boolean allCepSchools(List<String> cdsCodes) {
    try {
      var normalizedCdsCodes = cdsCodes.stream().map(Long::parseLong).toList();
      return SchoolListParser.cdsCodesForCepSchools().containsAll(normalizedCdsCodes);
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
