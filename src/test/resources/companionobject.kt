class Mob(
    private val name: String,
    private val health: Double
) {
    companion object {

        const val canSpawnMob: Boolean = false

        fun spawn(name: String, health: Double) = Mob(name, health)
    }
}