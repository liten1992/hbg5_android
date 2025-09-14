package priv.liten.hbg5_data

open class HBG5Data<TRequest, TResponse>: HBG5BaseData {

    constructor(): super()

    var request: TRequest?
        get() { return baseRequest as? TRequest }
        set(value) { baseRequest = value }

    var response: TResponse?
        get() { return baseResponse as? TResponse }
        set(value) { baseResponse = value }
}