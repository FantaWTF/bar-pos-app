package com.barpos.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.barpos.data.database.entity.Transaction
import com.barpos.data.database.entity.TransactionItem
import com.barpos.data.database.entity.TransactionType
import java.io.File
import java.io.FileWriter

object CsvExporter {

    fun exportTransactions(
        context: Context,
        transactions: List<Transaction>,
        transactionItems: Map<Long, List<TransactionItem>>,
        memberNames: Map<Long, String>
    ): File {
        val file = File(context.cacheDir, "bar_export_${System.currentTimeMillis()}.csv")
        FileWriter(file).use { writer ->
            writer.append("Dato;Medlem;Type;Produkt;Antal;Stykpris;Total\n")
            for (transaction in transactions) {
                val memberName = memberNames[transaction.memberId] ?: "Ukendt"
                val typeName = if (transaction.type == TransactionType.PURCHASE) "Køb" else "Betaling"
                val items = transactionItems[transaction.id]
                if (items != null && items.isNotEmpty()) {
                    for (item in items) {
                        writer.append(
                            "${DateUtils.formatDateTime(transaction.createdAt)};" +
                                    "$memberName;$typeName;${item.productName};" +
                                    "${item.quantity};${item.unitPrice};${item.quantity * item.unitPrice}\n"
                        )
                    }
                } else {
                    writer.append(
                        "${DateUtils.formatDateTime(transaction.createdAt)};" +
                                "$memberName;$typeName;;;${transaction.totalAmount}\n"
                    )
                }
            }
        }
        return file
    }

    fun shareFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Del CSV-fil"))
    }
}
