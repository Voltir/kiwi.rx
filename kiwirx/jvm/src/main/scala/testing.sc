import scala.collection.mutable
import kiwirx2._
import cassowary.rx.core._
import rx._


val x1 = Var(0.0)
val x2 = Var(0.0)
val x3 = Var(0.0)
val p = Var(0.0)
val s1 = new Slack
val s2 = new Slack
val objective = Row(x1 -> -7.0.coeff, x2 -> -8.0.coeff, x3 -> -10.0.coeff, p -> 1.0.coeff)
val fab = Row(1000.0.coeff)(x1 -> 2.0.coeff, x2 -> 3.0.coeff, x3 -> 2.0.coeff).insert(s1,1.0.coeff)
val assemble = Row(800.0.coeff)(x1 -> 1.0.coeff, x2 -> 1.0.coeff, x3 -> 2.0.coeff).insert(s2,1.0.coeff)
pprint.pprintln(List(objective,fab,assemble))

//Simplex.solve(objective)(fab,assemble)

//Simplex.pivotElement(objective).map {pivot =>  Simplex.iteration(objective,pivot)(fab,assemble)}
//Simplex.pivotElement(objective).map {pivot =>  Simplex.iteration(objective,pivot)(fab,assemble)}
//Simplex.pivotElement(objective).map {pivot =>  Simplex.iteration(objective,pivot)(fab,assemble)}
//
//pprint.pprintln(List(objective,fab,assemble))
//
//println(objective)

println("****")
Simplex.solve(objective)(fab,assemble)

println(objective)
