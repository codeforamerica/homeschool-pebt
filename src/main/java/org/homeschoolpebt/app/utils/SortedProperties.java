package org.homeschoolpebt.app.utils;

import org.apache.commons.collections4.MapUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.*;

public class SortedProperties extends Properties {
  public synchronized Set<Map.Entry<Object, Object>> entrySet() {
    var sorted = new TreeMap<Object, Object>();
    for (Map.Entry<Object, Object> entry : super.entrySet()) {
      sorted.put(entry.getKey(), entry.getValue());
    }
    System.out.println("ok sorted");
    return sorted.entrySet();
  }
  public synchronized Set<Object> keySet() {
    var sorted = new TreeSet<Object>();
    sorted.addAll(super.keySet());
    System.out.println("should be sortin");
    return sorted;
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new Exception("Usage: progname infilename outfilename");
    }
    String infilename = args[0];
    String outfilename = args[1];
    var tempbuf = new ByteArrayOutputStream();
    SortedProperties sp = new SortedProperties();
    sp.load(new java.io.FileInputStream(infilename));
    sp.store(tempbuf, null);
    Scanner scanner = new Scanner(new ByteArrayInputStream(tempbuf.toByteArray()));
    scanner.nextLine(); // Remove first line since Java adds timestamp
    var outfile = new FileOutputStream(outfilename);
    while(scanner.hasNextLine()) {
      String next = scanner.nextLine();
      outfile.write(next.getBytes());
      outfile.write("\n".getBytes());
      System.out.println("outputted line " + next);
    }
    outfile.close();
  }
}
