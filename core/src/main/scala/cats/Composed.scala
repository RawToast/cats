package cats

import cats.functor._

private[cats] trait ComposedInvariant[F[_], G[_]] extends Invariant[Lambda[A => F[G[A]]]] { outer =>
  def F: Invariant[F]
  def G: Invariant[G]

  override def imap[A, B](fga: F[G[A]])(f: A => B)(g: B => A): F[G[B]] =
    F.imap(fga)(ga => G.imap(ga)(f)(g))(gb => G.imap(gb)(g)(f))
}

private[cats] trait ComposedFunctor[F[_], G[_]] extends Functor[Lambda[A => F[G[A]]]] with ComposedInvariant[F, G] { outer =>
  def F: Functor[F]
  def G: Functor[G]

  override def map[A, B](fga: F[G[A]])(f: A => B): F[G[B]] =
    F.map(fga)(ga => G.map(ga)(f))
}

private[cats] trait ComposedApply[F[_], G[_]] extends Apply[Lambda[A => F[G[A]]]] with ComposedFunctor[F, G] { outer =>
  def F: Apply[F]
  def G: Apply[G]

  override def ap[A, B](fgf: F[G[A => B]])(fga: F[G[A]]): F[G[B]] =
    F.ap(F.map(fgf)(gf => G.ap(gf)(_)))(fga)

  override def product[A, B](fga: F[G[A]], fgb: F[G[B]]): F[G[(A, B)]] =
    F.map2(fga, fgb)(G.product)
}

private[cats] trait ComposedApplicative[F[_], G[_]] extends Applicative[Lambda[A => F[G[A]]]] with ComposedApply[F, G] { outer =>
  def F: Applicative[F]
  def G: Applicative[G]

  override def pure[A](x: A): F[G[A]] = F.pure(G.pure(x))
}

private[cats] trait ComposedSemigroupK[F[_], G[_]] extends SemigroupK[Lambda[A => F[G[A]]]] { outer =>
  def F: SemigroupK[F]

  override def combineK[A](x: F[G[A]], y: F[G[A]]): F[G[A]] = F.combineK(x, y)
}

private[cats] trait ComposedMonoidK[F[_], G[_]] extends MonoidK[Lambda[A => F[G[A]]]] with ComposedSemigroupK[F, G] { outer =>
  def F: MonoidK[F]

  override def empty[A]: F[G[A]] = F.empty
}

private[cats] trait ComposedAlternative[F[_], G[_]] extends Alternative[Lambda[A => F[G[A]]]] with ComposedApplicative[F, G] with ComposedMonoidK[F, G] { outer =>
  def F: Alternative[F]
}

private[cats] trait ComposedFoldable[F[_], G[_]] extends Foldable[Lambda[A => F[G[A]]]] { outer =>
  def F: Foldable[F]
  def G: Foldable[G]

  override def foldLeft[A, B](fga: F[G[A]], b: B)(f: (B, A) => B): B =
    F.foldLeft(fga, b)((b, a) => G.foldLeft(a, b)(f))

  override def foldRight[A, B](fga: F[G[A]], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
    F.foldRight(fga, lb)((ga, lb) => G.foldRight(ga, lb)(f))
}

private[cats] trait ComposedTraverse[F[_], G[_]] extends Traverse[Lambda[A => F[G[A]]]] with ComposedFoldable[F, G] with ComposedFunctor[F, G] { outer =>
  def F: Traverse[F]
  def G: Traverse[G]

  override def traverse[H[_]: Applicative, A, B](fga: F[G[A]])(f: A => H[B]): H[F[G[B]]] =
    F.traverse(fga)(ga => G.traverse(ga)(f))
}

private[cats] trait ComposedReducible[F[_], G[_]] extends Reducible[Lambda[A => F[G[A]]]] with ComposedFoldable[F, G] { outer =>
  def F: Reducible[F]
  def G: Reducible[G]

  override def reduceLeftTo[A, B](fga: F[G[A]])(f: A => B)(g: (B, A) => B): B = {
    def toB(ga: G[A]): B = G.reduceLeftTo(ga)(f)(g)
    F.reduceLeftTo(fga)(toB) { (b, ga) =>
      G.foldLeft(ga, b)(g)
    }
  }

  override def reduceRightTo[A, B](fga: F[G[A]])(f: A => B)(g: (A, Eval[B]) => Eval[B]): Eval[B] = {
    def toB(ga: G[A]): B = G.reduceRightTo(ga)(f)(g).value
    F.reduceRightTo(fga)(toB) { (ga, lb) =>
      G.foldRight(ga, lb)(g)
    }
  }
}

private[cats] trait ComposedContravariant[F[_], G[_]] extends Functor[Lambda[A => F[G[A]]]] { outer =>
  def F: Contravariant[F]
  def G: Contravariant[G]

  override def map[A, B](fga: F[G[A]])(f: A => B): F[G[B]] =
    F.contramap(fga)(gb => G.contramap(gb)(f))
}

private[cats] trait ComposedContravariantCovariant[F[_], G[_]] extends Contravariant[Lambda[A => F[G[A]]]] { outer =>
  def F: Contravariant[F]
  def G: Functor[G]

  override def contramap[A, B](fga: F[G[A]])(f: B => A): F[G[B]] =
    F.contramap(fga)(gb => G.map(gb)(f))
}

private[cats] trait ComposedCovariantContravariant[F[_], G[_]] extends Contravariant[Lambda[A => F[G[A]]]] { outer =>
  def F: Functor[F]
  def G: Contravariant[G]

  override def contramap[A, B](fga: F[G[A]])(f: B => A): F[G[B]] =
    F.map(fga)(ga => G.contramap(ga)(f))
}

private[cats] trait ComposedInvariantCovariant[F[_], G[_]] extends Invariant[Lambda[A => F[G[A]]]] { outer =>
  def F: Invariant[F]
  def G: Functor[G]

  override def imap[A, B](fga: F[G[A]])(f: A => B)(g: B => A): F[G[B]] =
    F.imap(fga)(ga => G.map(ga)(f))(gb => G.map(gb)(g))
}

private[cats] trait ComposedInvariantContravariant[F[_], G[_]] extends Invariant[Lambda[A => F[G[A]]]] { outer =>
  def F: Invariant[F]
  def G: Contravariant[G]

  override def imap[A, B](fga: F[G[A]])(f: A => B)(g: B => A): F[G[B]] =
    F.imap(fga)(ga => G.contramap(ga)(g))(gb => G.contramap(gb)(f))
}
