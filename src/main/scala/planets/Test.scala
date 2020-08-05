package planets

import zio._
import zio.console._
import zio.stream.ZStream
import better.files._

object Test extends App {

  val helloWorld = ZStream.fromChunk(Chunk.fromArray("Hello world".getBytes))

  val prg =
    for {
      nodes <- Ipfs.addFiles(("src" / "main").glob("**/*.scala"))
      _ <- ZIO.foreach(nodes) { node =>
        putStrLn(s"remote  cid: ${node.hash}") *>
        Ipfs.cat(node.hash)
          .flatMap(_.runCollect)
          .flatMap(x => putStrLn(new String(x.toArray)))
      }
    } yield ()

  def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    prg.provideLayer(Ipfs.layer("/ip4/127.0.0.1/tcp/5001") ++ Console.live).exitCode
}
