package app.urbanflo.urbanflosumoserver.netconvert

/**
 * Exception for errors in [runNetconvert], which is when `netconvert` returns a non-zero exit status.
 */
class NetconvertException(message: String) : Exception(message)