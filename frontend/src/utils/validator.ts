export const validateEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
};

export const wrongEmailFormatString = "The email you have inserted is not formatted properly.\nA valid email should be in the format:\nemail@something.domain";