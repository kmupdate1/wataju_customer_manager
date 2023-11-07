package jp.wataju.csv

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.ktor.http.content.*
import io.ktor.server.config.*
import jp.wataju.model.Customer
import jp.wataju.model.CustomerService
import jp.wataju.util.Properties
import kotlinx.coroutines.runBlocking

class Writer(
    private val databaseConfig: ApplicationConfig
) {
    private val date = java.util.Date().toString()

    suspend fun write(
        multiPartData: MultiPartData,
        accountId: java.util.UUID
    ) {
        multiPartData.forEachPart { partData ->
            if (partData is PartData.FileItem) {
                partData.streamProvider().use { inputStream ->
                    csvReader().open(inputStream) {
                        readAllWithHeaderAsSequence().forEach {
                            runBlocking {
                                CustomerService(databaseConfig).create(
                                    Customer(
                                        id = null,
                                        firstName = it.values.elementAt(0),
                                        familyName = it.values.elementAt(1),
                                        firstNameKana = it.values.elementAt(2),
                                        familyNameKana = it.values.elementAt(3),
                                        zipcodeWard = it.values.elementAt(4),
                                        zipcodeTown = it.values.elementAt(5),
                                        prefecture = it.values.elementAt(6),
                                        address1 = it.values.elementAt(7),
                                        address2 = it.values.elementAt(8),
                                        address3 = it.values.elementAt(9),
                                        phoneNumber = it.values.elementAt(10),
                                        mailAddress = it.values.elementAt(11),
                                        createDate = date,
                                        updateDate = date,
                                        createAccount = accountId,
                                        updateAccount = accountId
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

val familyName = Properties.getText("FAMILY_NAME")
val firstName = Properties.getText("FIRST_NAME")
val familyNameKana = Properties.getText("FAMILY_NAME_KANA")
val firstNameKana = Properties.getText("FIRST_NAME_KANA")
val zipcodeWard = Properties.getText("ZIPCODE_WARD")
val zipcodeTown = Properties.getText("ZIPCODE_TOWN")
val prefecture = Properties.getText("PREFECTURE")
val address1 = Properties.getText("ADDRESS_1")
val address2 = Properties.getText("ADDRESS_2")
val address3 = Properties.getText("ADDRESS_3")
val phoneNumber = Properties.getText("PHONE_NUMBER")
val mailAddress = Properties.getText("MAIL_ADDRESS")
