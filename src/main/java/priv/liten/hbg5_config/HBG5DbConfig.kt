package priv.liten.hbg5_config

object HBG5DbConfig {

    enum class FieldType {
        Autoincrement,
        String,
        UniqueString,
        Bool,
        Int,
        UniqueInt,
        Long,
        UniqueLong,
        Float,
        Double;

        val value : kotlin.String
            get() {
                return when(this) {
                    Autoincrement -> "INTEGER PRIMARY KEY AUTOINCREMENT"
                    String -> "TEXT"
                    UniqueString -> "TEXT UNIQUE"
                    Bool -> "INT2"
                    Int, Long -> "INTEGER"
                    UniqueInt, UniqueLong -> "INTEGER UNIQUE"
                    Float, Double -> "REAL"
                }
            }
    }
}