package com.kantu.pab_volunteers.data.repository

import com.kantu.pab_volunteers.data.firebase.FirestoreManager
import com.kantu.pab_volunteers.data.model.ImpactStats
import com.kantu.pab_volunteers.utils.Constants
import kotlinx.coroutines.tasks.await

class ImpactStatsRepository {

    private val db = FirestoreManager.db

    private fun statsDocument() = db.collection(Constants.COLLECTION_IMPACT_STATS)
        .document(Constants.IMPACT_STATS_DOC_ID)

    suspend fun getStats(): ImpactStats {
        return statsDocument().get().await().toObject(ImpactStats::class.java) ?: ImpactStats()
    }

    suspend fun saveStats(stats: ImpactStats): Result<Unit> {
        return try {
            statsDocument().set(stats).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
