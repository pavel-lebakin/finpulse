package com.lebakin.finpulse.server.auth

internal fun validateAccount(account: AuthAccount) {
    require(validId(account.id)) { "Auth account is invalid." }
    require(validEmail(account.email)) { "Auth account is invalid." }
    require(validVerifier(account.verifier)) { "Auth verifier is invalid." }
}

internal fun validEmail(email: String): Boolean {
    val lengthOk = email.length in MIN_EMAIL_LENGTH..MAX_EMAIL_LENGTH
    val at = email.indexOf('@')
    val oneAt = at > 0 && at == email.lastIndexOf('@') && at < email.lastIndex
    val domain = if (oneAt) email.substring(at + 1) else ""
    val domainOk = oneAt && validDomain(domain)
    return lengthOk && email.all(::isPlainText) && domainOk
}

internal fun validPassword(password: CharArray): Boolean {
    val lengthOk = password.size in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH
    return lengthOk && password.all(::isPlainText)
}

internal fun sameEmail(left: String, right: String): Boolean = left.equals(right, ignoreCase = true)

private fun validVerifier(verifier: PasswordVerifier): Boolean {
    val iterationsOk = verifier.iterations == PASSWORD_ITERATIONS
    val saltOk = verifier.salt.size == SALT_BYTES
    return iterationsOk && saltOk && verifier.hash.size == HASH_BYTES
}

internal fun validId(id: String): Boolean = id.length in 1..MAX_ID_LENGTH && id.all(::isIdChar)

private fun validDomain(domain: String): Boolean {
    val dotted = domain.contains('.') && !domain.startsWith('.') && !domain.endsWith('.')
    return dotted && !domain.contains("..")
}

private fun isPlainText(char: Char): Boolean =
    char in ' '..'~' && char != '"' && char != '\\' && char != ' '

private fun isIdChar(char: Char): Boolean =
    char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char == '_' || char == '-'
