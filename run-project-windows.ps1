# Run docker-compose commands
docker-compose pull
docker-compose up -d --wait

# Move to the frontend directory
cd "frontend"

# Install npm dependencies and start the development server
npm install
npm run dev

# Open the browser to localhost:5173
Start-Process "http://localhost:5173"
