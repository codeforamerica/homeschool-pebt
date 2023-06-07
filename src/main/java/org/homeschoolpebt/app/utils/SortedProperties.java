package org.homeschoolpebt.app.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.*;

public class SortedProperties extends Properties {
  public synchronized Set<Object> keySet() {
    var sorted = new TreeSet<Object>();
    sorted.addAll(super.keySet());
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
    Scanner fileScanner = new Scanner(new ByteArrayInputStream(tempbuf.toByteArray()));
    fileScanner.nextLine(); // Remove first line since Java adds timestamp
    var outfile = new FileOutputStream(outfilename);
    while(fileScanner.hasNextLine()) {
      String next = fileScanner.nextLine();
      outfile.write(next.getBytes());
    }
    outfile.close();
  }
}
