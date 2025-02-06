export const defineSseEventSource = (userId: string) => {
    console.log("Id retrieved:", userId);
    let eventSourceUrl = 'http://localhost:8081/notifications?id=' + userId;
    const eventSource = new EventSource(eventSourceUrl);
    console.log("defined event source for sse:", eventSourceUrl);
    return eventSource;
}