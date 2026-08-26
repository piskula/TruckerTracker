package sk.momosilabs.truckTrack.file.service

import sk.momosilabs.truckTrack.file.model.FileModel

interface FilePersistence {
    fun create(model: FileModel): FileModel
    fun delete(fileId: Long)
}
