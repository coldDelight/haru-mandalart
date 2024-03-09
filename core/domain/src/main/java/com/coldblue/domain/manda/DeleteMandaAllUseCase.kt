package com.coldblue.domain.manda

import com.coldblue.data.repo.MandaDetailRepository
import com.coldblue.data.repo.MandaKeyRepository
import com.coldblue.model.MandaKey
import javax.inject.Inject

class DeleteMandaAllUseCase @Inject constructor(
    private val mandaKeyRepository: MandaKeyRepository,
    private val mandaDetailRepository: MandaDetailRepository
) {
    suspend operator fun invoke(){
        mandaKeyRepository.deleteAllMandaDetail()
        mandaDetailRepository.deleteAllMandaDetail()
    }
}