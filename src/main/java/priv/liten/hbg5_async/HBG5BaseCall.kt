package priv.liten.hbg5_async

import android.widget.Switch

abstract class HBG5BaseCall {

    enum class Status {
        READY, RUNNING, COMPLETED, CANCELED;

        override fun toString(): String {
            return this.name
        }
    }

    /** 當執行 */
    @Throws(Exception::class)
    open fun onExecute() { }

    /** 當取消 */
    @Throws(Exception::class)
    open fun onCancel() { }

    /** 當發生例外 */
    open fun onException(exception: Exception?) { }

    /** 執行 執行邏輯 (非 NONE 則不進行邏輯)  */
    @Synchronized
    @Throws(Exception::class)
    open fun execute() {
        if (status != Status.READY) {
            throw Exception("Call can't execute because status is not none")
        }
        status = Status.RUNNING
        try {
            onExecute()
            if (status == Status.RUNNING) {
                throwFailExceptionIfNeed()
            }
        }
        catch (error: Exception) {
            try {
                catchException(error = error)
            }
            catch (error: Exception) {
                onException(error)
                throw error
            }
        }
        finally {
            status = Status.COMPLETED
        }
    }

    /** 取消 (已經 FINISH 則不可取消) */
    @Throws(Exception::class)
    open fun cancel() {
        if (status == Status.COMPLETED) {
            return
        }
        if (status != Status.READY && status != Status.RUNNING) {
            throw Exception("Call can't cancel because status is not none or running")
        }
        if (status == Status.READY) {
            status = Status.CANCELED
            return
        }
        try {
            onCancel()
        } catch (error: Exception) {
            throw error
        } finally {
            status = Status.CANCELED
        }
    }

    /** 執行狀態 */
    var status = Status.READY
        private set

    /** 判斷返回結果失敗的話拋出例外 */
    @Throws(Exception::class)
    open fun throwFailExceptionIfNeed() { }
    /** 判斷拋出的例外是否最終判定為例外狀況 */
    @Throws(Exception::class)
    open fun catchException(error: Exception) { throw error }
}