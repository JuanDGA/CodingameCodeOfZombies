import kotlin.math.*

import java.util.*

const val ASH_SPEED = 1000
const val ZOMBIE_SPEED = 400

fun main() {
  val engine = Engine(Scanner(System.`in`))
  repeat(Short.MAX_VALUE.toInt()) {
    engine.nextTurn()
    engine.compute()
    engine.execute()
  }
}

data class Vector2D(var x: Double, var y: Double) {
  constructor(x: Int, y: Int) : this(x.toDouble(), y.toDouble())
  constructor(from: Coordinate, to: Coordinate) : this(to.x - from.x, to.y - from.y)

  fun norm() = sqrt(((x * x) + (y * y)))

  fun isZero() = this.x == 0.0 && this.y == 0.0

  fun project(over: Vector2D): Vector2D {
    return over.scaled(this.dot(over) / over.norm().pow(2))
  }

  fun normalized(): Vector2D {
    if (norm() == 0.0) return zero()
    return this.scaled(1 / norm())
  }

  operator fun minus(other: Vector2D): Vector2D {
    return Vector2D(this.x - other.x, this.y - other.y)
  }

  operator fun plus(other: Vector2D): Vector2D {
    return Vector2D(this.x + other.x, this.y + other.y)
  }

  operator fun plusAssign(other: Vector2D) {
    this.x += other.x
    this.y += other.y
  }

  private fun dot(other: Vector2D): Double {
    return this.x * other.x + this.y * other.y
  }

  fun scaled(by: Double): Vector2D {
    return Vector2D(this.x * by, this.y * by)
  }

  fun rounded(): Vector2D {
    return Vector2D(this.x.toInt(), this.y.toInt())
  }

  companion object {
    fun zero(): Vector2D {
      return Vector2D(0, 0)
    }
  }
}

data class Coordinate(val x: Int, val y: Int) {
  fun distanceTo(other: Coordinate) = Vector2D(this, other).norm()

  fun distanceTo(x: Int, y: Int) = Vector2D(this, Coordinate(x, y)).norm()

  operator fun plus(other: Coordinate) = Coordinate(this.x + other.x, this.y + other.y)

  operator fun plus(other: Vector2D): Coordinate {
    return Coordinate(this.x + other.x.roundToInt(), this.y + other.y.roundToInt())
  }

  override fun toString() = "$x $y"

  companion object {
    fun zero(): Coordinate {
      return Coordinate(0, 0)
    }

    fun from(input: Scanner) = Coordinate(input.nextInt(), input.nextInt())
  }
}

enum class EntityType {
  Zombie,
  Human,
  Ash
}

data class Entity(
  val id: Int,
  val coordinate: Coordinate,
  val futureCoordinate: Coordinate = coordinate,
  val type: EntityType,
) {
  companion object {
    fun from(input: Scanner, type: EntityType): Entity {
      return when (type) {
        EntityType.Ash -> Entity(-1, Coordinate.from(input), type = type)
        EntityType.Human -> Entity(input.nextInt(), Coordinate.from(input), type = type)
        EntityType.Zombie -> Entity(input.nextInt(), Coordinate.from(input), Coordinate.from(input), type = type)
      }
    }
  }
}

class Engine(private val input: Scanner) {
  private lateinit var ash: Entity
  private var humansAlive: Int = 0
  private lateinit var humans: Array<Entity>
  private var zombiesAlive: Int = 0
  private lateinit var zombies: Array<Entity>

  private var target = Coordinate.zero()

  fun nextTurn() {
    ash = Entity.from(input, EntityType.Ash)
    humansAlive = input.nextInt()
    humans = Array(humansAlive) { Entity.from(input, EntityType.Human)  }
    zombiesAlive = input.nextInt()
    zombies = Array(zombiesAlive) { Entity.from(input, EntityType.Zombie)  }
  }

  fun compute() {
    val dangerousZombie = zombies
      .map { it to (humans + ash).minBy { human -> human.coordinate.distanceTo(it.coordinate) } }
      .filter {
        val (zombie, human) = it
        ((ash.coordinate.distanceTo(human.coordinate) - 2000) / ASH_SPEED).toInt() <= ((zombie.coordinate.distanceTo(human.coordinate) - 400) / ZOMBIE_SPEED).toInt()
      }
      .minByOrNull {
        val (zombie, human) = it
        zombie.coordinate.distanceTo(human.coordinate)
      }?.first ?: zombies.firstOrNull() ?: ash

    target = dangerousZombie.futureCoordinate
  }

  fun execute() {
    val direction = Vector2D(ash.coordinate, target).normalized()
    println(ash.coordinate + direction.scaled(ASH_SPEED.toDouble()).rounded())
  }
}