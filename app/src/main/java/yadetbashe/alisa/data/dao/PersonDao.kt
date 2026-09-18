package yadetbashe.app.alisa.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import yadetbashe.app.alisa.data.model.Person
import yadetbashe.app.alisa.data.model.PersonBalanceRow

@Dao
interface PersonDao {

    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun getAllPersons(): LiveData<List<Person>>

    @Query("SELECT * FROM persons ORDER BY name ASC")
    suspend fun getAllPersonsOnce(): List<Person>

    @Query("SELECT * FROM persons WHERE id = :id")
    suspend fun getPersonById(id: Long): Person?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person): Long

    @Update
    suspend fun updatePerson(person: Person)

    @Delete
    suspend fun deletePerson(person: Person)

    /** پشتیبان‌گیری و بازیابی */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(persons: List<Person>)
}
