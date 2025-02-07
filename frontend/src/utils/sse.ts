export const defineSseEventSource = (userId: string, ip: string = "localhost", port: string) => {
    console.log("Id retrieved:", userId);
    let eventSourceUrl = `http://${ip}:${port}/notifications?id=${userId}`;
    const eventSource = new EventSource(eventSourceUrl);
    console.log("defined event source for sse:", eventSourceUrl);
    return eventSource;
}