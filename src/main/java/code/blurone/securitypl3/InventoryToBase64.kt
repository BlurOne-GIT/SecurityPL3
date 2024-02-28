package code.blurone.securitypl3

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

// https://www.spigotmc.org/threads/how-do-i-save-a-players-inventory.38692/#post-472317
object InventoryToBase64 {
    fun toBase64(inventory: Inventory): String {
        try {
            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)

            // Write the size of the inventory
            dataOutput.writeInt(inventory.size)

            // Save every element in the list
            for (i in 0 until inventory.size) {
                dataOutput.writeObject(inventory.getItem(i))
            }


            // Serialize that array
            dataOutput.close()
            return Base64Coder.encodeLines(outputStream.toByteArray())
        } catch (e: Exception) {
            throw IllegalStateException("Unable to save item stacks.", e)
        }
    }

    @Throws(IOException::class)
    fun fromBase64(data: String?): Array<ItemStack?> /*Inventory*/ {
        try {
            val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(data))
            val dataInput = BukkitObjectInputStream(inputStream)
            //val inventory = Bukkit.getServer().createInventory(null, dataInput.readInt())
            val inventorySize = dataInput.readInt()
            val itemStacks = mutableListOf<ItemStack?>()

            // Read the serialized inventory
            for (i in 0 until inventorySize) {
                //inventory.setItem(i, dataInput.readObject() as ItemStack)
                itemStacks.add(dataInput.readObject() as ItemStack?)
            }
            dataInput.close()
            //return inventory
            return itemStacks.toTypedArray()
        } catch (e: ClassNotFoundException) {
            throw IOException("Unable to decode class type.", e)
        }
    }
}