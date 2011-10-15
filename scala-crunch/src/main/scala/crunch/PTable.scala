package crunch

import com.cloudera.crunch.{GroupingOptions, PTable => JTable, Pair => JPair}

class PTable[K, V](jtable: JTable[K, V]) extends PCollection[JPair[K, V]](jtable) with JTable[K, V] {

  override def getPTableType() = jtable.getPTableType()

  override def getKeyType() = jtable.getKeyType()

  override def getValueType() = jtable.getValueType()

  def filter(f: (Any, Any) => Boolean): PTable[K, V] = {
    ClosureCleaner.clean(f)
    parallelDo(new SFilterTableFn[K, V](f), getPTableType())
  }

  def map[T: ClassManifest](f: (Any, Any) => T) = {
    ClosureCleaner.clean(f)
    parallelDo(new STableMapFn[K, V, T](f), getPType(classManifest[T]))
  }

  def map[L: ClassManifest, W: ClassManifest](f: (Any, Any) => (L, W)) = {
    val ptf = getTypeFamily()
    val keyType = getPType(classManifest[L])
    val valueType = getPType(classManifest[W])
    ClosureCleaner.clean(f)
    parallelDo(new STableMapTableFn[K, V, L, W](f), ptf.tableOf(keyType, valueType))
  }

  def flatMap[T: ClassManifest](f: (Any, Any) => Seq[T]) = {
    ClosureCleaner.clean(f)
    parallelDo(new STableDoFn[K, V, T](f), getPType(classManifest[T]))
  }

  def flatMap[L: ClassManifest, W: ClassManifest](f: (Any, Any) => Seq[(L, W)]) = {
    val ptf = getTypeFamily()
    val keyType = getPType(classManifest[L])
    val valueType = getPType(classManifest[W])
    ClosureCleaner.clean(f)
    parallelDo(new STableDoTableFn[K, V, L, W](f), ptf.tableOf(keyType, valueType))
  }

  override def union(tables: JTable[K, V]*) = {
    new PTable[K, V](jtable.union(tables.map(baseCheck): _*))
  }

  private def baseCheck(c: JTable[K, V]): JTable[K, V] = c match {
    case x: PTable[K, V] => x.base.asInstanceOf[PTable[K, V]]
    case _ => c
  }

  def ++ (other: JTable[K, V]) = union(other)

  override def groupByKey() = new PGroupedTable(jtable.groupByKey())

  override def groupByKey(partitions: Int) = new PGroupedTable(jtable.groupByKey(partitions))

  override def groupByKey(options: GroupingOptions) = new PGroupedTable(jtable.groupByKey(options))
}
