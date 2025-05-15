package japanesedictionary

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import kotlinx.coroutines.test.runTest
import org.junit.Before


class DomainModelTesting {

    lateinit var items: List<BaseItem>

    @Before
    fun initialize()  = runTest {
        items = listOf()
    }


}