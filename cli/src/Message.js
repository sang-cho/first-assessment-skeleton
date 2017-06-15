export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ username, command, contents,timeStamp }) {
    this.username = username
    this.command = command
    this.contents = contents
    this.timeStamp=timeStamp
  }

  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents,
      timeStamp: this.timeStamp
    })
  }

  toString () {
    return this.contents
  }
}
