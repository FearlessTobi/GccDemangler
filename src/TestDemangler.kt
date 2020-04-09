import de.fearlesstobi.demangler.Demangler

object TestDemangler {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Hello world!")
        println(Demangler.Companion.parse("_ZN2nn4diag6detail10VAbortImplEPKcS3_S3_iPKNS_6ResultEPKNS_2os17UserExceptionInfoES3_St9__va_list"))
    }
}