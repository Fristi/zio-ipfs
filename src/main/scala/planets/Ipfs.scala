package planets

import java.io.InputStream
import java.util
import java.util.Optional

import better.files.File
import io.ipfs.api.NamedStreamable.FileWrapper
import io.ipfs.api.{IPFS, MerkleNode, NamedStreamable}
import io.ipfs.multihash.Multihash
import zio._
import zio.blocking.Blocking
import zio.stream.ZStream

import scala.jdk.CollectionConverters._

object Ipfs {

  trait Service {
    def addFiles(files: Iterator[File]): Task[List[MerkleNode]]
    def addBytes(stream: ZStream[Any, Throwable, Byte]): Task[List[MerkleNode]]
    def ls(hash: Multihash): Task[List[MerkleNode]]
    def cat(hash: Multihash): ZStream[Any, Throwable, Byte]
  }

  private def live(ipfs: IPFS, blocker: Blocking) = new Service {


    def addFiles(files: Iterator[File]): Task[List[MerkleNode]] =
      blocker.get.blocking(ZIO.effect(ipfs.add(files.toList.map(file => new FileWrapper(file.toJava) : NamedStreamable).asJava, false, false)).map(_.asScala.toList))

    def addBytes(stream: ZStream[Any, Throwable, Byte]): Task[List[MerkleNode]] = {
      stream.toInputStream.use { is =>
        val ns = new NamedStreamable {
          def getInputStream: InputStream = is
          def getName: Optional[String] = Optional.empty()
          def getChildren: util.List[NamedStreamable] = List.empty.asJava
          def isDirectory: Boolean = false
        }

        blocker.get.blocking(ZIO.effect(ipfs.add(ns).asScala.toList))
      }
    }

    def ls(hash: Multihash): Task[List[MerkleNode]] = blocker.get.blocking(ZIO.effect(ipfs.ls(hash))).map(_.asScala.toList)

    def cat(hash: Multihash): ZStream[Any, Throwable, Byte] =
      ZStream.fromInputStream(ipfs.catStream(hash)).provide(blocker)
  }

  def addFiles(files: Iterator[File]): RIO[Ipfs, List[MerkleNode]] =
    ZIO.accessM(_.get.addFiles(files))

  def addBytes(stream: ZStream[Any, Throwable, Byte]): RIO[Ipfs, List[MerkleNode]] =
    ZIO.accessM(_.get.addBytes(stream))

  def ls(hash: Multihash): RIO[Ipfs, List[MerkleNode]] =
    ZIO.accessM(_.get.ls(hash))

  def cat(hash: Multihash): RIO[Ipfs, ZStream[Any, Throwable, Byte]] =
    ZIO.access(_.get.cat(hash))



  def layer(addr: String): ZLayer[Any, Nothing, Ipfs] =
    Blocking.live >>> ZLayer.fromFunction(blocker => live(new IPFS(addr), blocker))

}
