package emergencymanager.backend.database.ops

import cats.effect._

import org.mongodb.scala.SingleObservable

trait MongoObservableOps {

    implicit class SingleObservableOps[A](obs: SingleObservable[A]) {
        def toIO(implicit ctx: ContextShift[IO]) = IO
            .fromFuture(
                IO(obs.toFuture())
            )
    }
}