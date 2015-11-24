package scraper.expressions

import scala.util.Try

import scraper.TypeMismatchException
import scraper.expressions.Cast.promoteDataTypes
import scraper.types.PrimitiveType

trait BinaryComparison extends Predicate with BinaryExpression {
  protected lazy val ordering: Ordering[Any] = whenStrictlyTyped {
    left.dataType match {
      case t: PrimitiveType =>
        t.ordering.asInstanceOf[Ordering[Any]]
    }
  }

  override lazy val strictlyTyped: Try[Expression] = for {
    lhs <- left.strictlyTyped map {
      case PrimitiveType(e) => e
      case e                => throw TypeMismatchException(e, classOf[PrimitiveType], None)
    }
    rhs <- right.strictlyTyped map {
      case PrimitiveType(e) => e
      case e                => throw TypeMismatchException(e, classOf[PrimitiveType], None)
    }
    (promotedLhs, promotedRhs) <- promoteDataTypes(lhs, rhs)
    newChildren = promotedLhs :: promotedRhs :: Nil
  } yield if (sameChildren(newChildren)) this else makeCopy(newChildren)
}

case class Eq(left: Expression, right: Expression) extends BinaryComparison {
  override def nullSafeEvaluate(lhs: Any, rhs: Any): Any = lhs == rhs

  override def annotatedString: String = s"(${left.annotatedString} = ${right.annotatedString})"

  override def sql: String = s"(${left.sql} = ${right.sql})"
}

case class NotEq(left: Expression, right: Expression) extends BinaryComparison {
  override def nullSafeEvaluate(lhs: Any, rhs: Any): Any = lhs != rhs

  override def annotatedString: String = s"(${left.annotatedString} != ${right.annotatedString})"

  override def sql: String = s"(${left.sql} != ${right.sql})"
}

case class Gt(left: Expression, right: Expression) extends BinaryComparison {
  override def nullSafeEvaluate(lhs: Any, rhs: Any): Any = ordering.gt(lhs, rhs)

  override def annotatedString: String = s"(${left.annotatedString} > ${right.annotatedString})"

  override def sql: String = s"(${left.sql} > ${right.sql})"
}

case class Lt(left: Expression, right: Expression) extends BinaryComparison {
  override def nullSafeEvaluate(lhs: Any, rhs: Any): Any = ordering.lt(lhs, rhs)

  override def annotatedString: String = s"(${left.annotatedString} < ${right.annotatedString})"

  override def sql: String = s"(${left.sql} < ${right.sql})"
}

case class GtEq(left: Expression, right: Expression) extends BinaryComparison {
  override def nullSafeEvaluate(lhs: Any, rhs: Any): Any = ordering.gteq(lhs, rhs)

  override def annotatedString: String = s"(${left.annotatedString} >= ${right.annotatedString})"

  override def sql: String = s"(${left.sql} >= ${right.sql})"
}

case class LtEq(left: Expression, right: Expression) extends BinaryComparison {
  override def nullSafeEvaluate(lhs: Any, rhs: Any): Any = ordering.lteq(lhs, rhs)

  override def annotatedString: String = s"(${left.annotatedString} <= ${right.annotatedString})"

  override def sql: String = s"(${left.sql} <= ${right.sql})"
}
