import zio.Has

package object planets {
  type Ipfs = Has[Ipfs.Service]
}
