# akka-pusher-play-app

This is the sample Play framework app using [akka-pusher](/dtaniwaki/akka-pusher).

```bash
export PUSHER_APP_ID="YOUR APP ID"
export PUHSER_API_KEY="YOUR APP KEY"
export PUSHER_API_SECRET="YOUR API SECRET"
sbt run
```

And access http://localhost:9000/pusher

Working sample is [available on Heroku](https://akka-pusher-play-app.herokuapp.com/pusher).

If you want to try the app by yourself,

[![Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy?template=https://github.com/dtaniwaki/akka-pusher-play-app)

You need to set the required environment variables above in Heroku as well.

