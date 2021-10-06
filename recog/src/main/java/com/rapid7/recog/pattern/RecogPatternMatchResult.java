package com.rapid7.recog.pattern;

/**
 * The result of a match operation.
 */
public interface RecogPatternMatchResult {

  /**
   * Returns the number of capturing groups in this result.
   */
  int groupCount();

  /**
   * Returns the input captured by the indexed group.
   *
   * @param index The index of the capturing group. Group indexes start at one.
   * @return The input captured by the group at the specified index, or {@code null}
   *     if there is no matching input for this group.
   * @throws IndexOutOfBoundsException if the index is less than 1 or greater than
   *     that returned of {@code groupCount()}.
   */
  String group(int index);

  /**
   * Returns the input captured by the named group.
   *
   * @param name The name of the capturing group.
   * @return Input captured by the named group or {@code null} if there is no
   *     matching input for this group.
   * @throws IllegalArgumentException if there is no group with this name.
   */
  String group(String name);

}
