package com.hieuwu.groceriesstore.data.repository.impl

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hieuwu.groceriesstore.data.database.dao.CategoryDao
import com.hieuwu.groceriesstore.data.database.entities.Category
import com.hieuwu.groceriesstore.data.database.entities.asDomainModel
import com.hieuwu.groceriesstore.data.repository.CategoryRepository
import com.hieuwu.groceriesstore.utilities.CollectionNames
import com.hieuwu.groceriesstore.utilities.convertCategoryDocumentToEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(private val categoryDao: CategoryDao) :
    CategoryRepository {

    override suspend fun refreshDatabase() {
        val categoriesList = mutableListOf<Category>()
        val fireStore = Firebase.firestore
        fireStore.collection(CollectionNames.categories).get().addOnSuccessListener { result ->
            for (document in result) {
                categoriesList.add(convertCategoryDocumentToEntity(document))
            }
        }.addOnFailureListener { exception ->
            Timber.w("Error getting documents.$exception")
        }.await()

        withContext(Dispatchers.IO) {
            categoryDao.insertAll(categoriesList)
        }
    }

    override fun getFromLocal() = categoryDao.getAll().map { it.asDomainModel() }

}
