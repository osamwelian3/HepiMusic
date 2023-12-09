package com.hepimusic.main.admin.categories

import android.app.Application
import androidx.annotation.StringRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Category
import com.hepimusic.R
import com.hepimusic.main.admin.albums.AdminAlbumsViewModel
import com.hepimusic.main.admin.common.BaseAdminViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AdminCategoriesViewModel @Inject constructor(
    val application: Application
): BaseAdminViewModel(application) {

    private val observable = AdminCategoriesObservable()

    fun getObservable(): AdminCategoriesViewModel.AdminCategoriesObservable {
        return observable
    }

    inner class AdminCategoriesObservable: BaseObservable() {
        private val _data = MutableLiveData<WriteResult>()
        internal val data: LiveData<WriteResult> get() = _data

        val _categoryToEdit = MutableLiveData<Category>()
        val categoryToEdit : LiveData<Category> = _categoryToEdit

        private val _key = MutableLiveData<String>()
        @Bindable
        val _name = MutableLiveData<String>()

        val key: LiveData<String> = _key
        val name: LiveData<String> = _name

        fun saveCategoryToDb(category: Category) {
            Amplify.DataStore.save(
                category,
                {
                    _data.postValue(
                        WriteResult(
                            true,
                            R.string.category_saved
                        )
                    )
                },
                {
                    _data.postValue(WriteResult(true))
                }
            )
        }

        fun createCategory() {
            val uuid = UUID.randomUUID().toString()
            val category = Category.builder()
                .key(uuid)
                .name(name.value)
                .build()
            saveCategoryToDb(category)
        }

        fun editCategory() {
            val category = categoryToEdit.value!!.copyOfBuilder()
                .name(name.value)
                .build()
            saveCategoryToDb(category)
        }

        fun clearResult(@StringRes success: Int) {
            _data.postValue(WriteResult(false, success))
        }
    }
}

internal data class WriteResult(val success: Boolean, @StringRes val message: Int? = null)