package crunch

import com.cloudera.crunch.{FilterFn, Pair => JPair};

class SFilterTableFn[K, V](f: (Any, Any) => Boolean) extends FilterFn[JPair[K, V]] {
  override def accept(input: JPair[K, V]): Boolean = {
    f(Conversions.c2s(input.first()), Conversions.c2s(input.second()));
  }
}
