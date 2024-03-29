package com.rapid7.recog;

import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;

public class TestGenerators {
  
  private static Random random = new Random();

  public static String anyUTF8String() {
    return anyUTF8String(16);
  }
  public static String anyUTF8String(int length) {
    return RandomStringUtils.random(length, 0, 0x10ffff, true, true);
  }

  public static <E extends Enum<E>> E anyEnum(Class<E> enumType) {
    if (enumType == null)
      throw new IllegalArgumentException("The enum class type must not be null.");

    E[] enumConstants = enumType.getEnumConstants();
    return enumConstants[random.nextInt(enumConstants.length)];
  }
}
