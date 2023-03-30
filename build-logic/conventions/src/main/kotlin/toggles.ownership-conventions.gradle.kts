plugins {
    id("se.premex.ownership")
}

ownership {
    validateOwnership = true
    generateGithubOwners = true
    generateMissingOwnershipFiles = true
    defaultOwnerForMissingOwnershipFiles = "@erikeelde"
}
