import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server


let thishost='localhost'
let thisport='8080'

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [host] [port]')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {

    //changes host/port if args are present. Ignore otherwise
    if(args.host!=null && args.port !=null) {
       thisport=args.port
       thishost=args.host}

    username = args.username
    server = connect({ host: thishost, port: thisport }, () => {
    server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {
      this.log(Message.fromJSON(buffer).toString())
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input)
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
     } else if (command === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      }
      else if (command ==='broadcast'){
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
      }
      else {
      this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })
