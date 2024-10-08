package com.fuciple0.jjikgo.level

class LevelSystem {
    private val experienceTable = mutableMapOf<Int, Int>()

    init {
        generateExperienceTable()
    }

    // 경험치 테이블 생성 (레벨마다 10씩 증가, 시작 경험치 20)
    private fun generateExperienceTable() {
        var baseExperience = 20
        for (level in 1..1000) {  // 최대 1000레벨까지 가능, 필요시 더 늘릴 수 있음
            experienceTable[level] = baseExperience
            baseExperience += 10  // 각 레벨마다 10 경험치씩 더 필요
        }
    }

    // 특정 레벨에서 다음 레벨로 가기 위한 경험치 반환
    fun getExperienceForLevel(level: Int): Int {
        return experienceTable[level] ?: 0  // 해당 레벨이 없으면 0 반환
    }
}
