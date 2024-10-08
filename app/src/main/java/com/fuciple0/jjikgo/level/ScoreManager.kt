package com.fuciple0.jjikgo.level

class ScoreManager(private val emailIndex: Int) {
    private var currentLevel = 1  // 현재 레벨 (초기 1)
    private var currentExperience = 0  // 현재 경험치
    private val levelSystem = LevelSystem()  // 레벨 시스템 클래스 인스턴스 생성

    // 활동에 따른 점수 추가 메서드
    fun addScoreForAction(actionType: String) {
        val score = when (actionType) {
            "share_post" -> 5  // 글 공유
            "like" -> 1  // 좋아요
            "follow" -> 3  // 팔로우
            "followed" -> 3  // 팔로잉
            else -> 0  // 기본값
        }

        addScore(score)
    }

    // 점수 추가 및 레벨업 체크
    private fun addScore(score: Int) {
        currentExperience += score
        checkForLevelUp()  // 레벨업 체크
    }

    // 레벨업을 처리하는 메서드
    private fun checkForLevelUp() {
        var requiredExperience = levelSystem.getExperienceForLevel(currentLevel)

        // 현재 경험치가 필요한 경험치를 초과하면 레벨업
        while (currentExperience >= requiredExperience) {
            currentExperience -= requiredExperience  // 경험치 차감
            currentLevel++  // 레벨업

            println("레벨업! 현재 레벨: $currentLevel")

            // 다음 레벨의 경험치 요구량 업데이트
            requiredExperience = levelSystem.getExperienceForLevel(currentLevel)
        }
    }

    // 현재 레벨 반환
    fun getCurrentLevel(): Int {
        return currentLevel
    }

    // 현재 경험치 반환
    fun getCurrentExperience(): Int {
        return currentExperience
    }

    // 다음 레벨까지 필요한 경험치 반환
    fun getExperienceToNextLevel(): Int {
        val requiredExperience = levelSystem.getExperienceForLevel(currentLevel)
        return requiredExperience - currentExperience
    }
}
